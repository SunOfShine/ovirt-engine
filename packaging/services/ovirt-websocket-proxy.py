#!/usr/bin/python

# Copyright 2013 Red Hat
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

import os
import sys
import signal
import gettext
import base64
import json
import datetime
_ = lambda m: gettext.dgettext(message=m, domain='ovirt-engine')


from M2Crypto import X509


import service
import config


import websockify


class OvirtWebSocketProxy(websockify.WebSocketProxy):
    """"
    Websocket proxy for usage with oVirt engine.
    Leverages websocket.py by Joel Martin
    """

    def __init__(self, *args, **kwargs):
        self._ticketDecoder = kwargs.pop('ticketDecoder')
        super(OvirtWebSocketProxy, self).__init__(*args, **kwargs)

    def get_target(self, target_cfg, path):
        """
        Parses the path, extracts a token, and looks for a valid
        target for that token in the configuration file(s). Sets
        target_host and target_port if successful
        """
        connection_data = self._ticketDecoder.decode(path[1:]).split(':')
        target_host = connection_data[0].encode('utf8')
        target_port = connection_data[1].encode('utf8')
        return (target_host, target_port)


class TicketDecoder(object):

    def __init__(
            self,
            insecure,
            certificate
    ):
        self._insecure = insecure
        if not insecure:
            self._key = X509.load_cert(
                certificate,
                X509.FORMAT_PEM,
            ).get_pubkey()

    def decode(self, ticket):
        decoded = json.loads(base64.b64decode(ticket))
        if not self._insecure:
            self._key.verify_init()
            for field in decoded['signedFields'].split(','):
                self._key.verify_update(decoded[field].encode('utf8'))
            if self._key.verify_final(
                base64.b64decode(decoded['signature'])
            ) != 1:
                raise ValueError('Invalid ticket signature')
        if (
            datetime.datetime.utcnow() -
            datetime.datetime.strptime(decoded['validFrom'], '%Y%m%d%H%M%S')
        ) < datetime.timedelta():
            raise ValueError('Ticket life time expired')
        if (
            datetime.datetime.strptime(decoded['validTo'], '%Y%m%d%H%M%S') -
            datetime.datetime.utcnow()
        ) < datetime.timedelta():
            raise ValueError('Ticket life time expired')
        return decoded['data']


class Daemon(service.Daemon):

    def __init__(self):
        super(Daemon, self).__init__()

    def _checkInstallation(
        self,
        pidfile,
    ):
        # Check the required engine directories and files:
        self.check(
            os.path.join(
                self._config.get('ENGINE_USR'),
                'services',
            ),
            directory=True,
        )

        if pidfile is not None:
            self.check(
                name=pidfile,
                writable=True,
                mustExist=False,
            )

    def daemonSetup(self):

        if not os.path.exists(config.ENGINE_WSPROXY_DEFAULT_FILE):
            raise RuntimeError(
                _(
                    "The configuration defaults file '{file}' "
                    "required but missing"
                ).format(
                    file=config.ENGINE_WSPROXY_DEFAULT_FILE,
                )
            )

        self._config = service.ConfigFile(
            (
                config.ENGINE_WSPROXY_DEFAULT_FILE,
                config.ENGINE_WSPROXY_VARS,
            ),
        )

        self._checkInstallation(
            pidfile=self.pidfile,
        )

    def daemonContext(self):
        #
        # WORKAROUND-BEGIN
        # set terminate exception as
        # the websockify library assumes interactive
        # mode only (SIGINT).
        # it also expect exit at the middle of processing.
        # so we comply.
        def myterm(signo, frame):
            sys.exit(0)
        oldterm = signal.getsignal(signal.SIGTERM)
        signal.signal(signal.SIGTERM, myterm)
        # WORKAROUND-END

        try:
            OvirtWebSocketProxy(
                listen_host=self._config.get('PROXY_HOST'),
                listen_port=self._config.get('PROXY_PORT'),
                source_is_ipv6=self._config.getboolean('SOURCE_IS_IPV6'),
                verbose=self.debug,
                ticketDecoder=TicketDecoder(
                    insecure=not self._config.getboolean(
                        'FORCE_DATA_VERIFICATION'
                    ),
                    certificate=self._config.get(
                        'CERT_FOR_DATA_VERIFICATION'
                    )
                ),
                cert=self._config.get('SSL_CERTIFICATE'),
                key=self._config.get('SSL_KEY'),
                ssl_only=self._config.getboolean('SSL_ONLY'),
                daemon=False,
                record=(
                    None if not self._config.getboolean('TRACE_ENABLE')
                    else self._config.get('TRACE_FILE')
                ),
                web=None,
                target_cfg='/dummy',
                target_host=None,
                target_port=None,
                wrap_mode='exit',
                wrap_cmd=None
            ).start_server()
        # WORKAROUND-BEGIN
        # websockify exit because of signals.
        # redirect it to expected termination sequence.
        except SystemExit:
            self.logger.debug('SystemExit', exc_info=True)
        finally:
            signal.signal(signal.SIGTERM, oldterm)
        # WORKAROUND-END


if __name__ == '__main__':
    service.setupLogger()
    d = Daemon()
    d.run()


# vim: expandtab tabstop=4 shiftwidth=4

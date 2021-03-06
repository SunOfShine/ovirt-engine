#
# ovirt-engine-setup -- ovirt engine setup
# Copyright (C) 2013 Red Hat, Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#


"""Connection plugin."""


import os
import gettext
_ = lambda m: gettext.dgettext(message=m, domain='ovirt-engine-setup')


from otopi import constants as otopicons
from otopi import util
from otopi import plugin


from ovirt_engine_setup import constants as osetupcons
from ovirt_engine_setup import util as osetuputil
from ovirt_engine_setup import database


@util.export
class Plugin(plugin.PluginBase):
    """Connection plugin."""

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)

    @plugin.event(
        stage=plugin.Stages.STAGE_INIT,
    )
    def _init(self):
        self.environment.setdefault(
            osetupcons.DBEnv.HOST,
            None
        )
        self.environment.setdefault(
            osetupcons.DBEnv.PORT,
            None
        )
        self.environment.setdefault(
            osetupcons.DBEnv.SECURED,
            None
        )
        self.environment.setdefault(
            osetupcons.DBEnv.SECURED_HOST_VALIDATION,
            None
        )
        self.environment.setdefault(
            osetupcons.DBEnv.USER,
            None
        )
        self.environment.setdefault(
            osetupcons.DBEnv.PASSWORD,
            None
        )
        self.environment.setdefault(
            osetupcons.DBEnv.DATABASE,
            None
        )

        self.environment[osetupcons.DBEnv.CONNECTION] = None
        self.environment[osetupcons.DBEnv.STATEMENT] = None
        self.environment[osetupcons.DBEnv.NEW_DATABASE] = True
        self.environment[osetupcons.DBEnv.PGPASS_FILE] = None

    @plugin.event(
        stage=plugin.Stages.STAGE_SETUP,
        name=osetupcons.Stages.DB_CONNECTION_SETUP,
    )
    def _setup(self):

        dbovirtutils = database.OvirtUtils(plugin=self)
        dbovirtutils.detectCommands()

        dbenv = None
        config = osetuputil.ConfigFile([
            osetupcons.FileLocations.OVIRT_ENGINE_SERVICE_CONFIG
        ])
        if config.get('ENGINE_DB_PASSWORD') is not None:
            dbenv = {}
            for e, k in (
                (osetupcons.DBEnv.HOST, 'ENGINE_DB_HOST'),
                (osetupcons.DBEnv.PORT, 'ENGINE_DB_PORT'),
                (osetupcons.DBEnv.USER, 'ENGINE_DB_USER'),
                (osetupcons.DBEnv.PASSWORD, 'ENGINE_DB_PASSWORD'),
                (osetupcons.DBEnv.DATABASE, 'ENGINE_DB_DATABASE'),
            ):
                dbenv[e] = config.get(k)
            for e, k in (
                (osetupcons.DBEnv.SECURED, 'ENGINE_DB_SECURED'),
                (
                    osetupcons.DBEnv.SECURED_HOST_VALIDATION,
                    'ENGINE_DB_SECURED_VALIDATION'
                )
            ):
                dbenv[e] = config.getboolean(k)

        elif os.path.exists(
            osetupcons.FileLocations.LEGACY_PSQL_PASS_FILE
        ):
            self.logger.info('Existing database pgpass found')
            with open(
                osetupcons.FileLocations.LEGACY_PSQL_PASS_FILE,
                'r',
            ) as f:
                for l in f:
                    l = l.rstrip('\n')
                    if ':%s:' % 'postgres' not in l:
                        d = l.split(':')
                        if len(d) == 5:
                            dbenv = {
                                osetupcons.DBEnv.HOST: d[0],
                                osetupcons.DBEnv.PORT: int(d[1]),
                                osetupcons.DBEnv.SECURED: None,
                                osetupcons.DBEnv.SECURED_HOST_VALIDATION: None,
                                osetupcons.DBEnv.DATABASE: (
                                    d[2] if d[2] != '*'
                                    else
                                    osetupcons.Defaults.DEFAULT_DB_DATABASE
                                ),
                                osetupcons.DBEnv.USER: d[3],
                                osetupcons.DBEnv.PASSWORD: d[4],
                            }
                            break

        if dbenv is not None:
            try:
                self.environment[otopicons.CoreEnv.LOG_FILTER].append(
                    dbenv[osetupcons.DBEnv.PASSWORD]
                )
                dbovirtutils.tryDatabaseConnect(dbenv)
                self.environment.update(dbenv)
                self.environment[
                    osetupcons.DBEnv.NEW_DATABASE
                ] = dbovirtutils.isNewDatabase()
            except RuntimeError as e:
                self.logger.debug(
                    _(
                        'Cannot connect to database using existing '
                        'credentials: {user}@{host}:{port}, error: {error}'
                    ).format(
                        host=dbenv[osetupcons.DBEnv.HOST],
                        port=dbenv[osetupcons.DBEnv.PORT],
                        database=dbenv[osetupcons.DBEnv.DATABASE],
                        user=dbenv[osetupcons.DBEnv.USER],
                        error=e,
                    ),
                    exc_info=True,
                )


# vim: expandtab tabstop=4 shiftwidth=4

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


"""
Firewall configuration plugin for Engine.
"""

import gettext
_ = lambda m: gettext.dgettext(message=m, domain='ovirt-engine-setup')


from otopi import util
from otopi import plugin


from ovirt_engine_setup import constants as osetupcons


@util.export
class Plugin(plugin.PluginBase):
    """
    Firewall configuration plugin for Engine
    """

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)

    @plugin.event(
        stage=plugin.Stages.STAGE_CUSTOMIZATION,
        before=[
            osetupcons.Stages.NET_FIREWALL_MANAGER_PROCESS_TEMPLATES,
        ],
        after=[
            osetupcons.Stages.NET_FIREWALL_MANAGER_AVAILABLE,
            osetupcons.Stages.SYSTEM_NFS_CONFIG_AVAILABLE,
        ],
    )
    def _configuration(self):
        self.environment[osetupcons.NetEnv.FIREWALLD_SERVICES].extend([
            {
                'name': 'ovirt-http',
                'directory': 'base'
            },
            {
                'name': 'ovirt-https',
                'directory': 'base'
            },
        ])
        self.environment[
            osetupcons.NetEnv.FIREWALLD_SUBST
        ].update({
            '@HTTP_PORT@': self.environment[
                osetupcons.ConfigEnv.HTTP_PORT
            ],
            '@HTTPS_PORT@': self.environment[
                osetupcons.ConfigEnv.HTTPS_PORT
            ]
        })


# vim: expandtab tabstop=4 shiftwidth=4

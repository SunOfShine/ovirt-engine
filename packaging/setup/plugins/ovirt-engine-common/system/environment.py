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


"""Environment plugin."""


import grp
import os
import pwd
import gettext
_ = lambda m: gettext.dgettext(message=m, domain='ovirt-engine-setup')


from otopi import util
from otopi import plugin


from ovirt_engine_setup import constants as osetupcons


@util.export
class Plugin(plugin.PluginBase):
    """Environment plugin."""

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)

    @plugin.event(
        stage=plugin.Stages.STAGE_INIT,
    )
    def _init(self):
        if self.environment[osetupcons.CoreEnv.DEVELOPER_MODE]:
            rootUser = engineUser = apacheUser = pwd.getpwuid(os.geteuid())[0]
            engineGroup = grp.getgrgid(os.getegid())[0]
        else:
            rootUser = osetupcons.Defaults.DEFAULT_SYSTEM_USER_ROOT
            engineUser = osetupcons.Defaults.DEFAULT_SYSTEM_USER_ENGINE
            apacheUser = osetupcons.Defaults.DEFAULT_SYSTEM_USER_APACHE
            engineGroup = osetupcons.Defaults.DEFAULT_SYSTEM_GROUP_ENGINE

        self.environment.setdefault(
            osetupcons.SystemEnv.USER_ROOT,
            rootUser
        )
        self.environment.setdefault(
            osetupcons.SystemEnv.USER_ENGINE,
            engineUser
        )
        self.environment.setdefault(
            osetupcons.SystemEnv.USER_APACHE,
            apacheUser
        )
        self.environment.setdefault(
            osetupcons.SystemEnv.USER_VDSM,
            osetupcons.Defaults.DEFAULT_SYSTEM_USER_VDSM
        )
        self.environment.setdefault(
            osetupcons.SystemEnv.GROUP_KVM,
            osetupcons.Defaults.DEFAULT_SYSTEM_GROUP_KVM
        )
        self.environment.setdefault(
            osetupcons.SystemEnv.GROUP_ENGINE,
            engineGroup
        )
        self.environment.setdefault(
            osetupcons.SystemEnv.USER_POSTGRES,
            osetupcons.Defaults.DEFAULT_SYSTEM_USER_POSTGRES
        )


# vim: expandtab tabstop=4 shiftwidth=4

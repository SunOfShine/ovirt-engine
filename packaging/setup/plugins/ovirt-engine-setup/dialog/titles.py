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


"""Titles plugin."""


import gettext
_ = lambda m: gettext.dgettext(message=m, domain='ovirt-engine-setup')


from otopi import util
from otopi import plugin


from ovirt_engine_setup import constants as osetupcons


@util.export
class Plugin(plugin.PluginBase):
    """Titles plugin."""

    def _title(self, text):
        self.dialog.note(
            text='\n--== %s ==--\n\n' % text,
        )

    def __init__(self, context):
        super(Plugin, self).__init__(context=context)

    @plugin.event(
        stage=plugin.Stages.STAGE_CUSTOMIZATION,
        name=osetupcons.Stages.DIALOG_TITLES_S_NETWORK,
    )
    def _title_s_network(self):
        self._title(
            text=_('NETWORK CONFIGURATION'),
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_CUSTOMIZATION,
        name=osetupcons.Stages.DIALOG_TITLES_E_NETWORK,
        after=[
            osetupcons.Stages.DIALOG_TITLES_S_NETWORK,
        ],
    )
    def _title_e_network(self):
        pass

    @plugin.event(
        stage=plugin.Stages.STAGE_CUSTOMIZATION,
        name=osetupcons.Stages.DIALOG_TITLES_S_DATABASE,
        after=[
            osetupcons.Stages.DIALOG_TITLES_E_NETWORK,
        ],
    )
    def _title_s_database(self):
        self._title(
            text=_('DATABASE CONFIGURATION'),
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_CUSTOMIZATION,
        name=osetupcons.Stages.DIALOG_TITLES_E_DATABASE,
        after=[
            osetupcons.Stages.DIALOG_TITLES_S_DATABASE,
        ],
    )
    def _title_e_database(self):
        pass

    @plugin.event(
        stage=plugin.Stages.STAGE_CUSTOMIZATION,
        name=osetupcons.Stages.DIALOG_TITLES_S_ENGINE,
        after=[
            osetupcons.Stages.DIALOG_TITLES_E_DATABASE,
        ],
    )
    def _title_s_engine(self):
        self._title(
            text=_('OVIRT ENGINE CONFIGURATION'),
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_CUSTOMIZATION,
        name=osetupcons.Stages.DIALOG_TITLES_E_ENGINE,
        after=[
            osetupcons.Stages.DIALOG_TITLES_S_ENGINE,
        ],
    )
    def _title_e_engine(self):
        pass

    @plugin.event(
        stage=plugin.Stages.STAGE_CUSTOMIZATION,
        name=osetupcons.Stages.DIALOG_TITLES_S_PKI,
        after=[
            osetupcons.Stages.DIALOG_TITLES_E_ENGINE,
        ],
    )
    def _title_s_pki(self):
        self._title(
            text=_('PKI CONFIGURATION'),
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_CUSTOMIZATION,
        name=osetupcons.Stages.DIALOG_TITLES_E_PKI,
        after=[
            osetupcons.Stages.DIALOG_TITLES_S_PKI,
        ],
    )
    def _title_e_pki(self):
        pass

    @plugin.event(
        stage=plugin.Stages.STAGE_CUSTOMIZATION,
        name=osetupcons.Stages.DIALOG_TITLES_S_APACHE,
        after=[
            osetupcons.Stages.DIALOG_TITLES_E_PKI,
        ],
    )
    def _title_s_apache(self):
        self._title(
            text=_('APACHE CONFIGURATION'),
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_CUSTOMIZATION,
        name=osetupcons.Stages.DIALOG_TITLES_E_APACHE,
        after=[
            osetupcons.Stages.DIALOG_TITLES_S_APACHE,
        ],
    )
    def _title_e_apache(self):
        pass

    @plugin.event(
        stage=plugin.Stages.STAGE_CUSTOMIZATION,
        name=osetupcons.Stages.DIALOG_TITLES_S_SYSTEM,
        after=[
            osetupcons.Stages.DIALOG_TITLES_E_APACHE,
        ],
    )
    def _title_s_system(self):
        self._title(
            text=_('SYSTEM CONFIGURATION'),
        )

    @plugin.event(
        stage=plugin.Stages.STAGE_CUSTOMIZATION,
        name=osetupcons.Stages.DIALOG_TITLES_E_SYSTEM,
        after=[
            osetupcons.Stages.DIALOG_TITLES_S_SYSTEM,
        ],
    )
    def _title_e_system(self):
        self._title(
            text=_('END OF CONFIGURATION'),
        )


# vim: expandtab tabstop=4 shiftwidth=4

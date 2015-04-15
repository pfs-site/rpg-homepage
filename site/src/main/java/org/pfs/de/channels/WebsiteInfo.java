package org.pfs.de.channels;

import org.hippoecm.hst.configuration.channel.ChannelInfo;
import org.hippoecm.hst.core.parameters.FieldGroup;
import org.hippoecm.hst.core.parameters.FieldGroupList;
import org.hippoecm.hst.core.parameters.JcrPath;
import org.hippoecm.hst.core.parameters.Parameter;

/**
 * Retrieves the properties of a website channel.
 */
@FieldGroupList({
        @FieldGroup(
                titleKey = "fields.website",
                value = { "headerName", "bannerInformationPath" }
        )
})
public interface WebsiteInfo extends ChannelInfo {

    @Parameter(name = "headerName", defaultValue = "HST Website")
    String getHeaderName();

    @Parameter(name = "bannerInformationPath")
    @JcrPath(
            pickerSelectableNodeTypes = {"hippo:document"},
            pickerInitialPath = "cms-pickers/documents"
    )
    String getBannerInformationPath();

}

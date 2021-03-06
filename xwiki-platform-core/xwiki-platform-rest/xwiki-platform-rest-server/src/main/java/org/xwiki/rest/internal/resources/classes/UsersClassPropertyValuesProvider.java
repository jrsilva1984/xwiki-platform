/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.xwiki.rest.internal.resources.classes;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.query.QueryBuilder;
import org.xwiki.rest.model.jaxb.PropertyValues;
import org.xwiki.wiki.user.WikiUserManager;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiAttachment;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.classes.UsersClass;

/**
 * Provides values for the "List of Users" type of properties.
 * 
 * @version $Id$
 * @since 9.8
 */
@Component
@Named("Users")
@Singleton
public class UsersClassPropertyValuesProvider extends AbstractUsersAndGroupsClassPropertyValuesProvider<UsersClass>
{
    @Inject
    private WikiUserManager wikiUserManager;

    @Inject
    private QueryBuilder<UsersClass> allowedValuesQueryBuilder;

    @Override
    protected Class<UsersClass> getPropertyType()
    {
        return UsersClass.class;
    }

    @Override
    protected QueryBuilder<UsersClass> getAllowedValuesQueryBuilder()
    {
        return this.allowedValuesQueryBuilder;
    }

    @Override
    protected PropertyValues getAllowedValues(UsersClass propertyDefinition, int limit, String filter) throws Exception
    {
        String wikiId = propertyDefinition.getOwnerDocument().getDocumentReference().getWikiReference().getName();
        switch (this.wikiUserManager.getUserScope(wikiId)) {
            case LOCAL_AND_GLOBAL:
                return getLocalAndGlobalAllowedValues(propertyDefinition, limit, filter);
            case GLOBAL_ONLY:
                return getGlobalAllowedValues(propertyDefinition, limit, filter);
            default:
                return getLocalAllowedValues(propertyDefinition, limit, filter);
        }
    }

    @Override
    protected String getIcon(DocumentReference userReference)
    {
        XWikiContext xcontext = this.xcontextProvider.get();
        try {
            XWikiDocument userProfileDocument = xcontext.getWiki().getDocument(userReference, xcontext);
            String avatar = userProfileDocument.getStringValue("avatar");
            XWikiAttachment avatarAttachment = userProfileDocument.getAttachment(avatar);
            if (avatarAttachment != null && avatarAttachment.isImage(xcontext)) {
                return xcontext.getWiki().getURL(avatarAttachment.getReference(), "download",
                    "width=30&height=30&keepAspectRatio=true", null, xcontext);
            }
        } catch (XWikiException e) {
            this.logger.warn(
                "Failed to read the avatar of user [{}]. Root cause is [{}]. Using the default avatar instead.",
                userReference.getName(), ExceptionUtils.getRootCauseMessage(e));
        }

        return xcontext.getWiki().getSkinFile("icons/xwiki/noavatar.png", true, xcontext);
    }
}

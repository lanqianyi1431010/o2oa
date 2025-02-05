package com.x.portal.assemble.designer.jaxrs.file;

import com.x.base.core.project.tools.URLTools;
import org.apache.commons.lang3.StringUtils;

import com.google.gson.JsonElement;
import com.x.base.core.container.EntityManagerContainer;
import com.x.base.core.container.factory.EntityManagerContainerFactory;
import com.x.base.core.entity.JpaObject;
import com.x.base.core.entity.annotation.CheckPersistType;
import com.x.base.core.project.bean.WrapCopier;
import com.x.base.core.project.bean.WrapCopierFactory;
import com.x.base.core.project.cache.CacheManager;
import com.x.base.core.project.exception.ExceptionAccessDenied;
import com.x.base.core.project.exception.ExceptionDuplicateFlag;
import com.x.base.core.project.exception.ExceptionDuplicateRestrictFlag;
import com.x.base.core.project.exception.ExceptionEntityNotExist;
import com.x.base.core.project.http.ActionResult;
import com.x.base.core.project.http.EffectivePerson;
import com.x.base.core.project.jaxrs.WoId;
import com.x.base.core.project.tools.ListTools;
import com.x.portal.assemble.designer.Business;
import com.x.portal.core.entity.File;
import com.x.portal.core.entity.Portal;

class ActionCreate extends BaseAction {
	ActionResult<Wo> execute(EffectivePerson effectivePerson, JsonElement jsonElement) throws Exception {
		try (EntityManagerContainer emc = EntityManagerContainerFactory.instance().create()) {
			ActionResult<Wo> result = new ActionResult<>();
			Wi wi = this.convertToWrapIn(jsonElement, Wi.class);
			Business business = new Business(emc);
			Portal portal = emc.flag(wi.getPortal(), Portal.class);
			if (null == portal) {
				throw new ExceptionEntityNotExist(wi.getPortal(), Portal.class);
			}
			if (!business.editable(effectivePerson, portal)) {
				throw new ExceptionAccessDenied(effectivePerson);
			}
			File file = new File();
			Wi.copier.copy(wi, file);
			/** 设置file 的Id由前端生成提供 */
			file.setId(wi.getId());
			file.setPortal(portal.getId());
			this.updateCreator(file, effectivePerson);
			if (StringUtils.isEmpty(file.getName())) {
				throw new ExceptionEmptyName();
			}
			emc.beginTransaction(File.class);
			if (StringUtils.isNotEmpty(file.getAlias())) {
				if (emc.duplicateWithFlags(file.getId(), File.class, file.getAlias())) {
					throw new ExceptionDuplicateFlag(File.class, file.getAlias());
				}
			}
			if (emc.duplicateWithRestrictFlags(File.class, File.portal_FIELDNAME, file.getPortal(), file.getId(),
					ListTools.toList(file.getName()))) {
				throw new ExceptionDuplicateRestrictFlag(File.class, file.getName());
			}
			file.setShortUrlCode(getShortUrlCode(business, file, 6));
			emc.persist(file, CheckPersistType.all);
			emc.commit();
			CacheManager.notify(File.class);
			Wo wo = new Wo();
			wo.setId(file.getId());
			result.setData(wo);
			return result;
		}
	}



	public static class Wo extends WoId {
	}

	public static class Wi extends File {

		private static final long serialVersionUID = 4289841165185269299L;

		static WrapCopier<Wi, File> copier = WrapCopierFactory.wi(Wi.class, File.class, null,
				ListTools.toList(JpaObject.FieldsUnmodifyExcludeId, File.data_FIELDNAME, File.shortUrlCode_FIELDNAME));

	}

}

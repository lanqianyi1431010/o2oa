package com.x.portal.assemble.surface.jaxrs.file;

import java.util.List;

import com.x.base.core.container.EntityManagerContainer;
import com.x.base.core.container.factory.EntityManagerContainerFactory;
import com.x.base.core.entity.JpaObject;
import com.x.base.core.project.Applications;
import com.x.base.core.project.bean.WrapCopier;
import com.x.base.core.project.bean.WrapCopierFactory;
import com.x.base.core.project.exception.ExceptionEntityNotExist;
import com.x.base.core.project.http.ActionResult;
import com.x.base.core.project.http.EffectivePerson;
import com.x.base.core.project.tools.ListTools;
import com.x.base.core.project.x_portal_assemble_surface;
import com.x.portal.assemble.surface.Business;
import com.x.portal.core.entity.File;
import com.x.portal.core.entity.Portal;
import org.apache.commons.lang3.StringUtils;

class ActionListWithPortal extends BaseAction {

	ActionResult<List<Wo>> execute(EffectivePerson effectivePerson, String portalFlag) throws Exception {
		try (EntityManagerContainer emc = EntityManagerContainerFactory.instance().create()) {
			Business business = new Business(emc);
			ActionResult<List<Wo>> result = new ActionResult<>();
			Portal portal = business.portal().pick(portalFlag);
			if (null == portal) {
				throw new ExceptionEntityNotExist(portalFlag, Portal.class);
			}
			List<Wo> wos = emc.fetchEqual(File.class, Wo.copier, File.portal_FIELDNAME, portal.getId());
			wos.stream().forEach(wo -> {
				if(StringUtils.isBlank(wo.getShortUrlCode())){
					wo.setShortUrlCode(wo.getId());
				}
				wo.setFileUri("/"+Applications.joinQueryUri(x_portal_assemble_surface.class.getSimpleName(), "jaxrs", "file", wo.getShortUrlCode()));
			});
			wos = business.file().sort(wos);
			result.setData(wos);
			return result;
		}
	}

	public static class Wo extends File {

		private static final long serialVersionUID = 3121411589636528551L;
		static WrapCopier<File, Wo> copier = WrapCopierFactory.wo(File.class, Wo.class, null,
				ListTools.toList(JpaObject.FieldsInvisible, File.data_FIELDNAME));

		private String fileUri;

		public String getFileUri() {
			return fileUri;
		}

		public void setFileUri(String fileUri) {
			this.fileUri = fileUri;
		}
	}

}

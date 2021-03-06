package io.onedev.server.manager.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.criterion.Restrictions;

import io.onedev.server.manager.BuildQuerySettingManager;
import io.onedev.server.model.BuildQuerySetting;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.persistence.annotation.Transactional;
import io.onedev.server.persistence.dao.AbstractEntityManager;
import io.onedev.server.persistence.dao.Dao;
import io.onedev.server.persistence.dao.EntityCriteria;

@Singleton
public class DefaultBuildQuerySettingManager extends AbstractEntityManager<BuildQuerySetting> 
		implements BuildQuerySettingManager {

	@Inject
	public DefaultBuildQuerySettingManager(Dao dao) {
		super(dao);
	}

	@Sessional
	@Override
	public BuildQuerySetting find(Project project, User user) {
		EntityCriteria<BuildQuerySetting> criteria = newCriteria();
		criteria.add(Restrictions.and(Restrictions.eq("project", project), Restrictions.eq("user", user)));
		return find(criteria);
	}

	@Transactional
	@Override
	public void save(BuildQuerySetting setting) {
		if (setting.getUserQueries().isEmpty()) {
			if (!setting.isNew())
				delete(setting);
		} else {
			super.save(setting);
		}
	}

}

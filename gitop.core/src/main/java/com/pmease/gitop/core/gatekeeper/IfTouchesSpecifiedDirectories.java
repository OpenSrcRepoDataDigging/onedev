package com.pmease.gitop.core.gatekeeper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.pmease.commons.editable.annotation.Editable;
import com.pmease.commons.util.pattern.WildcardUtils;
import com.pmease.gitop.core.editable.DirectoryChoice;
import com.pmease.gitop.model.Branch;
import com.pmease.gitop.model.PullRequest;
import com.pmease.gitop.model.PullRequestUpdate;
import com.pmease.gitop.model.User;
import com.pmease.gitop.model.gatekeeper.FileGateKeeper;
import com.pmease.gitop.model.gatekeeper.checkresult.CheckResult;

@SuppressWarnings("serial")
@Editable(order=90, icon="icon-folder-submodule", description=
		"This gate keeper will be passed if any commit files are under specified directories.")
public class IfTouchesSpecifiedDirectories extends FileGateKeeper {

	private List<String> directories = new ArrayList<>();
	
	@Editable(name="Specify Directories", description="Use comma to separate multiple directories.")
	@DirectoryChoice
	@NotNull
	@Size(min=1, message="At least one directory has to be specified.")
	public List<String> getDirectories() {
		return directories;
	}

	public void setDirectories(List<String> directories) {
		this.directories = directories;
	}

	@Override
	public CheckResult doCheckRequest(PullRequest request) {
		for (int i=0; i<request.getEffectiveUpdates().size(); i++) {
			PullRequestUpdate update = request.getEffectiveUpdates().get(i);

			Collection<String> touchedFiles = request.getTarget().getProject().code()
					.listChangedFiles(update.getBaseCommit(), update.getHeadCommit());
			for (String file: touchedFiles) {
				for (String each: directories) {
					if (WildcardUtils.matchPath(each + "/**", file)) {
						request.setBaseUpdate(update);
						return accepted("Touched directory '" + each + "'.");
					}
				}
			}
		}

		return rejected("Not touched directories '" + getDirectories() + "'.");
	}

	@Override
	protected CheckResult doCheckFile(User user, Branch branch, @Nullable String file) {
		if (file != null) {
			for (String each: directories) {
				if (WildcardUtils.matchPath(each + "/**", file)) 
					return accepted("Touched directory '" + each + "'.");
			}
			return rejected("Not touched directories '" + getDirectories() + "'.");
		} else {
			return accepted("Touched specified directories.");
		}
	}

	@Override
	protected CheckResult doCheckCommit(User user, Branch branch, String commit) {
		for (String file: branch.getProject().code().listChangedFiles(branch.getHeadCommit(), commit)) {
			for (String each: directories) {
				if (WildcardUtils.matchPath(each + "/**", file))
					return accepted("Touched directory '" + each + "'.");
			}
		}

		return rejected("Not touched directories '" + getDirectories() + "'.");
	}

}

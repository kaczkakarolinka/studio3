package com.aptana.git.internal.core.storage;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.team.core.history.IFileHistoryProvider;
import org.eclipse.team.core.history.IFileRevision;
import org.eclipse.team.core.history.provider.FileHistory;

import com.aptana.git.core.model.GitCommit;
import com.aptana.git.core.model.GitRepository;
import com.aptana.git.core.model.GitRevList;
import com.aptana.git.core.model.GitRevSpecifier;

public class GitFileHistory extends FileHistory
{

	private IResource resource;
	private final CommitFileRevision[] revisions;

	public GitFileHistory(IResource resource, int flags, IProgressMonitor monitor)
	{
		this.resource = resource;
		this.revisions = buildRevisions(flags, monitor);
	}

	private CommitFileRevision[] buildRevisions(int flags, IProgressMonitor monitor)
	{
		SubMonitor subMonitor = SubMonitor.convert(monitor, 100);
		try
		{
			if (resource == null || resource.getProject() == null)
				return new CommitFileRevision[0];
			GitRepository repo = GitRepository.getAttached(this.resource.getProject());
			if (repo == null)
				return new CommitFileRevision[0];
			// Need the repo relative path
			String resourcePath = repo.relativePath(resource);
			List<IFileRevision> revisions = new ArrayList<IFileRevision>();
			GitRevList list = new GitRevList(repo);
			int max = -1;
			if ((flags & IFileHistoryProvider.SINGLE_REVISION) == IFileHistoryProvider.SINGLE_REVISION)
			{
				max = 1;
			}
			list.walkRevisionListWithSpecifier(new GitRevSpecifier(resourcePath), max, subMonitor.newChild(95));
			List<GitCommit> commits = list.getCommits();
			for (GitCommit gitCommit : commits)
			{
				revisions.add(new CommitFileRevision(gitCommit, resource.getProjectRelativePath().toPortableString()));
			}
			return revisions.toArray(new CommitFileRevision[revisions.size()]);
		}
		finally
		{
			subMonitor.done();
		}
	}

	public IFileRevision[] getContributors(IFileRevision revision)
	{
		if (!(revision instanceof CommitFileRevision))
			return new IFileRevision[0];
		CommitFileRevision arg = (CommitFileRevision) revision;
		List<IFileRevision> targets = new ArrayList<IFileRevision>();
		if (revisions != null)
		{
			for (CommitFileRevision aRevision : revisions)
			{
				if (arg.isDescendantOf(aRevision))
				{
					targets.add(aRevision);
				}
			}
		}
		return targets.toArray(new IFileRevision[targets.size()]);
	}

	public IFileRevision getFileRevision(String id)
	{
		if (revisions != null)
		{
			for (IFileRevision revision : revisions)
			{
				if (revision.getContentIdentifier().equals(id))
				{
					return revision;
				}
			}
		}
		return null;
	}

	public IFileRevision[] getFileRevisions()
	{
		final IFileRevision[] r = new IFileRevision[revisions.length];
		System.arraycopy(revisions, 0, r, 0, r.length);
		return r;
	}

	public IFileRevision[] getTargets(IFileRevision revision)
	{
		if (!(revision instanceof CommitFileRevision))
			return new IFileRevision[0];
		List<IFileRevision> targets = new ArrayList<IFileRevision>();
		if (revisions != null)
		{
			for (CommitFileRevision aRevision : revisions)
			{
				if (aRevision.isDescendantOf(revision))
				{
					targets.add(aRevision);
				}
			}
		}
		return targets.toArray(new IFileRevision[targets.size()]);
	}

}

using JetBrains.Annotations;
using RossyntBackend.Models;

namespace RossyntBackend.Repositories;

public interface IProjectRepository {
    public void SetTree(Tree tree);

    [Pure]
    public Tree? GetTree();

    public void RemoveTree();
}

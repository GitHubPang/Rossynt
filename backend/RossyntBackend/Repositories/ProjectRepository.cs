using System;
using JetBrains.Annotations;
using RossyntBackend.Models;

namespace RossyntBackend.Repositories {
    public sealed class ProjectRepository : IProjectRepository {
        private readonly object _lock = new object();
        private Tree? _tree;

        // ******************************************************************************** //

        public void SetTree(Tree tree) {
            lock (_lock) {
                _tree = tree ?? throw new ArgumentNullException(nameof(tree));
            }
        }

        [Pure]
        public Tree? GetTree() {
            lock (_lock) {
                return _tree;
            }
        }

        public void RemoveTree() {
            lock (_lock) {
                _tree = null;
            }
        }
    }
}

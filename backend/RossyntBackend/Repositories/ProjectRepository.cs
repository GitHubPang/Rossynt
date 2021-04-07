using System;
using JetBrains.Annotations;
using RossyntBackend.Models;

namespace RossyntBackend.Repositories {
    // ReSharper disable AnnotationRedundancyInHierarchy
    public sealed class ProjectRepository : IProjectRepository {
        [NotNull] private readonly object _lock = new object();
        [CanBeNull] private Tree _tree;

        // ******************************************************************************** //

        public void SetTree([NotNull] Tree tree) {
            lock (_lock) {
                _tree = tree ?? throw new ArgumentNullException(nameof(tree));
            }
        }

        [Pure]
        [CanBeNull]
        public Tree GetTree() {
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
    // ReSharper restore AnnotationRedundancyInHierarchy
}

using System;
using System.Threading.Tasks;
using JetBrains.Annotations;
using Microsoft.AspNetCore.Mvc;
using RoslynSyntaxTreeBackend.Models;
using RoslynSyntaxTreeBackend.Repositories;

namespace RoslynSyntaxTreeBackend.Controllers {
    [ApiController]
    [Route("[controller]")]
    public class SyntaxTreeController : ControllerBase {
        [NotNull] private readonly IProjectRepository _projectRepository;

        // ******************************************************************************** //

        public SyntaxTreeController([NotNull] IProjectRepository projectRepository) {
            _projectRepository = projectRepository ?? throw new ArgumentNullException(nameof(projectRepository));
        }

        [NotNull]
        [HttpPost(nameof(SetActiveFile))]
        public async Task SetActiveFile([NotNull] string projectId, [NotNull] string filePath) {
            if (projectId == null) throw new ArgumentNullException(nameof(projectId));
            if (filePath == null) throw new ArgumentNullException(nameof(filePath));

            var tree = await Tree.CompileFile(filePath, HttpContext.RequestAborted);
            var projectRecord = new ProjectRecord(projectId, tree);
            _projectRepository.SetProjectRecord(projectRecord);
        }
    }
}

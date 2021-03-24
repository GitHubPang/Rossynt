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

        [HttpPost(nameof(SetActiveFile))]
        [NotNull]
        public async Task<string> SetActiveFile([NotNull] [FromForm] SetActiveFileRequest request) {
            if (request == null) throw new ArgumentNullException(nameof(request));

            var tree = await Tree.CompileFile(request.FilePath, HttpContext.RequestAborted);
            var projectRecord = new ProjectRecord(request.ProjectId, tree);
            _projectRepository.SetProjectRecord(projectRecord);
            return "pqr";
        }
    }
}

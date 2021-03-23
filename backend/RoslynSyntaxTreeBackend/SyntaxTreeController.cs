using System;
using System.Threading.Tasks;
using Microsoft.AspNetCore.Mvc;

#nullable enable

namespace RoslynSyntaxTreeBackend {
    [ApiController]
    [Route("[controller]")]
    public class SyntaxTreeController : ControllerBase {
        private readonly IProjectRepository _projectRepository;

        // ******************************************************************************** //

        public SyntaxTreeController(IProjectRepository projectRepository) {
            _projectRepository = projectRepository ?? throw new ArgumentNullException(nameof(projectRepository));
        }

        [HttpPost(nameof(SetActiveFile))]
        public async Task SetActiveFile(string projectId, string filePath) {
            if (projectId == null) throw new ArgumentNullException(nameof(projectId));
            if (filePath == null) throw new ArgumentNullException(nameof(filePath));

            var tree = await Tree.CompileFile(filePath, HttpContext.RequestAborted);
            var projectRecord = new ProjectRecord(projectId, tree);
            _projectRepository.SetProjectRecord(projectRecord);
        }
    }
}

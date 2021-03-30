namespace RoslynSyntaxTreeBackend.ApplicationLifetime {
    public interface IApplicationLifetimeService {
        void StartCountdown();
        void RestartCountdown();
        void QuitApplication();
    }
}

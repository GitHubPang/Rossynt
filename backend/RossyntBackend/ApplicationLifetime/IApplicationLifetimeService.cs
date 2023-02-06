namespace RossyntBackend.ApplicationLifetime {
    public interface IApplicationLifetimeService {
        public void StartCountdown();
        public void RestartCountdown();
        public void QuitApplication();
    }
}

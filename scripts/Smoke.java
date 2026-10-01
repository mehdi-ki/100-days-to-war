import com.mehdi.daystowar.core.WorldSimulation;

public final class Smoke {
    private Smoke() {
    }

    public static void main(String[] args) {
        WorldSimulation simulation = new WorldSimulation(42L);
        for (int day = 1; day <= 10; day++) {
            require(simulation.chooseEvent(1).success(), "event choice failed");
            require(simulation.endDay().success(), "day transition failed");
        }
        require(simulation.state().finished(), "war test did not finish");
        require(simulation.state().report() != null, "war report missing");
        System.out.println("Core smoke test passed: ratio=" + simulation.state().report().ratio());
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }
}

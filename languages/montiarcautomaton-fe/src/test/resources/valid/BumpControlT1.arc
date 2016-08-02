package valid;

component BumpControlT1 {
  port
    in Double distance;

  implementation BumpControl {
    automaton BumpControl{
      state Idle, Driving, Backing, Turning;

      initial Idle;

      Idle -> Driving [distance < 2] / {};
    }
  }
}

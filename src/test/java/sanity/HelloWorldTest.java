package sanity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

class HelloWorldTest {
  @Test
  void helloworld() {
    String greeting = "Hello" + " " + "world";
    System.out.println(greeting);
    assertEquals(greeting, "Hello world");
  }
}

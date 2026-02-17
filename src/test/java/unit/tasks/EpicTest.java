package unit.tasks;

import org.junit.jupiter.api.Test;
import tracker.tasks.Epic;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class EpicTest {
    @Test
    public void epicCannotBeAddedToItself() {

        Epic task1 = new Epic(
                1L,
                "name",
                "desc"
        );

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        task1.addSubtaskId(1L)
        );
    }
}

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;

import java.net.URI;

public abstract class Download extends DefaultTask {
    // Use an abstract getter and setter method
    @Input
    abstract URI getUri();
    abstract void setUri(URI uri);

    @TaskAction
    void run() {
        // Use the `uri` property
        System.out.println("Downloading " + getUri());
    }
}

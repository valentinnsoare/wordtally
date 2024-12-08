package io.valentinsoare.wordtally.outputformat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.stereotype.Component;

/***
 * This OutputFormat class is tagged as a spring boot component to be scanned and injected where is needed automatically when the application is started.
 * As you can see, it is a singleton, and we're going to create only one instance from this class, and we will use it wherever we need to print the error messages in JSON format.
 * In other words with a Jackson library, we serialize the ErrorMessage objects into a String with JSON format, key:value pair.
 */

@Component
public class OutputFormat {
    private ObjectMapper jsonStyle;

    /**
     * Default constructor for OutputFormat class.
     */
    public OutputFormat() {}

    /**
     * This method returns an ObjectMapper configured to handle Java Time objects.
     * If the ObjectMapper has not been initialized, it creates a new one and registers the JavaTimeModule to it.
     *
     * @return An ObjectMapper configured to handle Java Time objects.
     */
    public ObjectMapper withJSONStyle() {
        if (jsonStyle == null) {
            jsonStyle = new ObjectMapper();
            jsonStyle.registerModule(new JavaTimeModule());
        }

        return jsonStyle;
    }
}

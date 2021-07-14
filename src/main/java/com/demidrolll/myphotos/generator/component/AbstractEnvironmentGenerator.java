package com.demidrolll.myphotos.generator.component;

import javax.ejb.embeddable.EJBContainer;
import javax.naming.Context;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

public abstract class AbstractEnvironmentGenerator {

    private final Properties environment = new Properties();
    private final Map<String, String> variables = new HashMap<>();

    protected final void execute() throws Exception {
        Map<String, Object> properties = setupProperties();
        try (EJBContainer ec = EJBContainer.createEJBContainer(properties)) {
            Context context = ec.getContext();
            context.bind("inject", this);
            generate();
        }
    }

    protected abstract void generate() throws Exception;

    private Map<String, Object> setupProperties() throws IOException {
        Map<String, Object> properties = new HashMap<>();
        readEnvironmentProperties();
        setupVariables();
        setupClasspathEnvironmentProperties(properties);
        properties.put(EJBContainer.MODULES, getModulePath());

        return properties;
    }

    private Object getModulePath() {
        String[] modules = environment.getProperty(EJBContainer.MODULES).split(",");
        List<File> files =
                Arrays.stream(modules).map(module -> new File(resolveModule(module))).collect(Collectors.toList());
        files.add(new File("target/classes"));
        return files.toArray();
    }

    private String resolveModule(String module) {
        String result = module;
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            result = result.replace(entry.getKey(), entry.getValue());
        }
        return result;
    }

    private void setupClasspathEnvironmentProperties(Map<String, Object> properties) {
        environment.forEach((key, value) -> properties.put((String) key, value));
    }

    private void setupVariables() {
        String userHome = System.getProperty("user.home");
        variables.put("${M2_LOCAL}", userHome + "/.m2/repository");
    }

    private void readEnvironmentProperties() throws IOException {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("environment.properties")) {
            environment.load(in);
        }
    }
}

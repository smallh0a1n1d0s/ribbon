package com.netflix.ribbonclientextensions.template;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;

/**
 * Created by mcohen on 5/1/14.
 */
public class TemplateParser {

    public static List<Object> parseTemplate(String template) {
        List<Object> templateParts = new ArrayList<Object>();
        if (template == null) {
            return templateParts;
        }
        StringBuilder val = new StringBuilder();
        String key;
        for (char c : template.toCharArray()) {
            switch (c) {
                case '{':
                    key = val.toString();
                    val = new StringBuilder();
                    templateParts.add(key);
                    break;

                case '}':
                    key = val.toString();
                    val = new StringBuilder();
                    if (key.charAt(0) == ';') {
                        templateParts.add(new MatrixVar(key.substring(1)));
                    } else {
                        templateParts.add(new PathVar(key));
                    }
                    break;
                default:
                    val.append(c);
            }
        }
        key = val.toString();
        if (!key.isEmpty()) {
            templateParts.add(key);
        }
        return templateParts;
    }

    public static String toData(Map<String, String> variables, String template, List<Object> parsedList) throws TemplateParsingException {
        int params = variables.size();
        // skip expansion if there's no valid variables set. ex. {a} is the
        // first valid
        if (variables.isEmpty() && template.indexOf('{') == 0) {
            return template;
        }

        StringBuilder builder = new StringBuilder();
        for (Object part : parsedList) {
            if (part instanceof TemplateVar) {
                String var = variables.get(part.toString());
                if (part instanceof MatrixVar) {
                    if (var != null) {
                        builder.append(';').append(part.toString()).append('=').append(var);
                        params--;
                    }
                } else if (part instanceof PathVar) {
                    if (var == null) {
                        throw new TemplateParsingException(String.format("template variable %s was not supplied for template %s", part.toString(), template));
                    } else {
                        builder.append(var);
                        params--;
                    }
                } else {
                    throw new TemplateParsingException(String.format("template variable type %s is not supplied for template template %s", part.getClass().getCanonicalName(), template));
                }
            } else {
                builder.append(part.toString());
            }
        }

        return builder.toString();
    }
    
    public static void main(String[] args) throws TemplateParsingException {
        String template = "/abc/{id}?name={name}";
        Map<String, String> vars = Maps.newHashMap();
        vars.put("id", "5");
        vars.put("name", "netflix");
        List<Object> list = parseTemplate(template);
        System.out.println(toData(vars, template, list));
        
    }
}

package org.xss.demo;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/personService")
public class PersonController {

    @PostMapping(value = "/person")
    private ResponseEntity<String> echoPerson(@RequestHeader Map<String, String> headers,
        @RequestParam String param, @RequestBody Person body) {
        ObjectNode response = JsonNodeFactory.instance.objectNode();
        headers.forEach((key, value) -> response.put(key, value));
        response.put("firstName", body.getFirstName());
        response.put("lastName", body.getLastName());
        response.put("age", body.getAge());
        response.put("param", param);
        return new ResponseEntity<String>(response.toString(), HttpStatus.OK);
    }

    @GetMapping(value = "/person")
    private ResponseEntity<String> echoParam(@RequestHeader Map<String, String> headers,
                                              @RequestParam String param) {
        ObjectNode response = JsonNodeFactory.instance.objectNode();
        headers.forEach((key, value) -> response.put(key, value));
        response.put("param", param);
        return new ResponseEntity<String>(response.toString(), HttpStatus.OK);
    }
}
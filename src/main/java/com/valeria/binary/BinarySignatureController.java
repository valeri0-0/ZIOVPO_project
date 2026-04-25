package com.valeria.binary;

import com.valeria.signature.dto.IdsRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/binary/signatures")
@RequiredArgsConstructor
public class BinarySignatureController {

    private final BinarySignatureService service;

    @GetMapping("/full")
    public ResponseEntity<?> getFull() {
        return service.getFull();
    }

    @GetMapping("/increment")
    public ResponseEntity<?> getIncrement(@RequestParam(required = false) Instant since) {

        if (since == null) {
            return ResponseEntity.badRequest().body("since обязателен");
        }

        return service.getIncrement(since);
    }

    @PostMapping("/by-ids")
    public ResponseEntity<?> getByIds(@RequestBody IdsRequest request) {
        return service.getByIds(request.getIds());
    }
}
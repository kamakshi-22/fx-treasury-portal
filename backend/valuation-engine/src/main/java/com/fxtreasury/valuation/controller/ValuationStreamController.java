package com.fxtreasury.valuation.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@RestController
@RequestMapping("/api/v1/stream")
//@CrossOrigin(origins = "http://localhost:5173")
public class ValuationStreamController {

    private static final Logger log = LoggerFactory.getLogger(ValuationStreamController.class);
    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    @GetMapping(value = "/valuations", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribeValuations() {
        SseEmitter emitter = new SseEmitter(1800_000L);
        emitters.add(emitter);
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError((e) -> emitters.remove(emitter));
        return emitter;
    }

    @KafkaListener(topics = "${app.kafka.topics.valuation-events}")
    public void broadcastValuation(String payload) {
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name("valuation-tick")
                        .data(payload, MediaType.APPLICATION_JSON));
            } catch (IOException e) {
                emitters.remove(emitter);
            }
        }
    }
}
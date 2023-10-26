package com.fgama.pillowtalk.service;

import com.fgama.pillowtalk.domain.Emo;
import com.fgama.pillowtalk.repository.EmoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmoService {

    private final EmoRepository emoRepository;

    public Long join(Emo emo) {
        emoRepository.save(emo);
        return emo.getId();
    }

    public List<Emo> findAll() {
        return emoRepository.findAll();
    }


    public Emo findOptionEmoByTitle(String title) throws NullPointerException {
        return emoRepository.findOptionEmoByTitle(title).orElseThrow(() -> new NullPointerException("title없음"));
    }


}
package com.jamsil_team.sugeun.service.phrase;

import com.jamsil_team.sugeun.domain.phrase.Phrase;
import com.jamsil_team.sugeun.dto.phrase.PhraseDTO;

public interface PhraseService {

    Phrase createPhrase(PhraseDTO phraseDTO);

    void ModifyPhraseText(Long phraseId, String text);

    void ModifyBookmark(Long phraseId);

    void removePhrase(Long phraseId);


}

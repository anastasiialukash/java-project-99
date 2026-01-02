package hexlet.code.service;

import hexlet.code.dto.LabelDTO;

import java.util.List;

public interface LabelService {
    List<LabelDTO> getAllLabels();
    LabelDTO getLabelById(Long id);
    LabelDTO getLabelByName(String name);
    LabelDTO createLabel(String name, String username);
    LabelDTO updateLabel(Long id, String name, String username);
    void deleteLabel(Long id, String username);
}

package serverside.interfaces;

import java.util.List;

public interface History {
    void SaveHistory(List<String> arr);
    List<String> loadHistory();
    List<String> Censor();
}

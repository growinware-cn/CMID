package cn.edu.nju.checker;

/**
 * Created by njucjc at 2018/1/10
 */
public class Result {
    private boolean value;

    private String link;

    public Result(boolean value, String link) {
        this.value = value;
        this.link = link;
    }

    public boolean getValue() {
        return value;
    }

    public void setValue(boolean value) {
        this.value = value;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }
}

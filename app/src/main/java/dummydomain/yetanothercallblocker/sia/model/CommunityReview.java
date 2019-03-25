package dummydomain.yetanothercallblocker.sia.model;

public class CommunityReview {

    private int id;

    private NumberRating rating;
    private NumberCategory category;
    private String author;
    private String title;
    private String comment;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public NumberRating getRating() {
        return rating;
    }

    public void setRating(NumberRating rating) {
        this.rating = rating;
    }

    public NumberCategory getCategory() {
        return category;
    }

    public void setCategory(NumberCategory category) {
        this.category = category;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    @Override
    public String toString() {
        return "CommunityReview{" +
                "id=" + id +
                ", rating=" + rating +
                ", category=" + category +
                ", author='" + author + '\'' +
                ", title='" + title + '\'' +
                ", comment='" + comment + '\'' +
                '}';
    }

}

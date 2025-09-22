package torrent;

public class AnswerTracker {
    Integer answer_status;
    Seeder answer_seeder;

    public AnswerTracker (Integer answer_status, Seeder answer_Seeder){
        this.answer_status = answer_status;
        this.answer_seeder = answer_Seeder;
    }

    public Integer getAnswerStatus(){
        return this.answer_status;
    }

    public Seeder getSeeder(){
        return this.answer_seeder;
    }

}

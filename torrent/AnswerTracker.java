package torrent;

/*
  Es utilizado por el tracker como respuesta a los solicitudes de los leechers.
  Si answer_status es -1 -> no hay seeder en la red que contenga el bloque solicitado. Por lo que answer_seeder es NULL.
  Caso contrario, answer_status = id_seeder y answer_seeder = referencia del seeder.
 */
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

package sb.tasks.jobs.trupd.metafile;

import java.util.Date;

public interface Mt {

    String name();

    Date creationDate();

    byte[] body();

    class Default implements Mt {

        private final Date created;

        public Default(Date created) {
            this.created = created;
        }

        @Override
        public String name() {
            return "empty";
        }

        @Override
        public Date creationDate() {
            return created;
        }

        @Override
        public byte[] body() {
            return new byte[0];
        }
    }
}

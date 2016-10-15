package org.gpc4j.ncaaf.writers;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import org.gpc4j.ncaaf.jaxb.Games;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Lyle T Harris
 */
@Provider
@Produces({MediaType.TEXT_PLAIN})
public class GamesWriter implements MessageBodyWriter<Games> {

    final static private org.slf4j.Logger LOG
            = LoggerFactory.getLogger(GamesWriter.class);


    @Override
    public boolean isWriteable(Class<?> type, Type genericType,
            Annotation[] annotations, MediaType mediaType) {
        LOG.debug(type.getName());
        return true;
    }


    @Override
    public long getSize(Games t, Class<?> type, Type genericType,
            Annotation[] annotations, MediaType mediaType) {
        LOG.debug(type.getName());
        return -1;
    }


    @Override
    public void writeTo(Games games, Class<?> type,
            Type genericType, Annotation[] annotations,
            MediaType mediaType, MultivaluedMap<String, Object> httpHeaders,
            OutputStream oStream) throws IOException, WebApplicationException {

        LOG.debug(type.getName());
        try (PrintWriter writer = new PrintWriter(oStream)) {
            games.getGame().stream().forEach(g -> {
                writer.append(g.getVisitor());
                writer.append("@");
                writer.append(g.getHome());
                writer.append(":\t");
                writer.append(g.getVisitorScore());
                writer.append(" to ");
                writer.append(g.getHomeScore());
                writer.append("\n");
            });
        }

    }


}

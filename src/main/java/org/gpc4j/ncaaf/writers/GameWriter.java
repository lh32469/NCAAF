package org.gpc4j.ncaaf.writers;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;
import org.gpc4j.ncaaf.jaxb.Game;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Lyle T Harris
 */
@Provider
@Produces({MediaType.APPLICATION_XML})
public class GameWriter implements MessageBodyWriter<Game> {

    final QName qname = new QName("http://jaxb.ncaaf.gpc4j.org", "game");

    final JAXBContext jc;

    final static private org.slf4j.Logger LOG
            = LoggerFactory.getLogger(GameWriter.class);


    public GameWriter() throws JAXBException {
        LOG.info(this.toString());
        jc = JAXBContext.newInstance(Game.class);
    }


    @Override
    public boolean isWriteable(Class<?> type, Type genericType,
            Annotation[] annotations, MediaType mediaType) {
        LOG.debug(type.getName());
        return true;
    }


    @Override
    public long getSize(Game t, Class<?> type,
            Type genericType, Annotation[] annotations, MediaType mediaType) {
        LOG.debug(type.getName());
        return -1;
    }


    @Override
    public void writeTo(Game game, Class<?> type,
            Type genericType, Annotation[] annotations,
            MediaType mediaType, MultivaluedMap<String, Object> httpHeaders,
            OutputStream oStream) throws IOException, WebApplicationException {

        LOG.debug(type.getName());

        try {

            JAXBElement jb = new JAXBElement(qname, Game.class, game);
            Marshaller m = jc.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            try (PrintWriter writer = new PrintWriter(oStream)) {
                m.marshal(jb, writer);
            }

        } catch (JAXBException ex) {
            LOG.error("Error", ex);
            throw new ProcessingException(
                    "Error serializing a Game to the output stream", ex);
        }

    }


}

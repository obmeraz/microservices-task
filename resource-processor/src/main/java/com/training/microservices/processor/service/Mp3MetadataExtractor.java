package com.training.microservices.processor.service;

import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaCoreProperties;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;

@Component
public class Mp3MetadataExtractor {

    private final Parser parser = new AutoDetectParser();

    public ExtractedMetadata extract(byte[] mp3Data) {
        try (TikaInputStream inputStream = TikaInputStream.get(new ByteArrayInputStream(mp3Data))) {
            Metadata metadata = new Metadata();
            metadata.set(TikaCoreProperties.RESOURCE_NAME_KEY, "audio.mp3");

            parser.parse(inputStream, new BodyContentHandler(-1), metadata, new ParseContext());

            String name = firstNonBlank(metadata, "title", "dc:title", "xmpDM:title");
            String artist = firstNonBlank(metadata, "xmpDM:artist", "artist", "Author");
            String album = firstNonBlank(metadata, "xmpDM:album", "album");
            String durationSeconds = firstNonBlank(metadata, "xmpDM:duration", "duration");
            String year = firstNonBlank(metadata, "xmpDM:releaseDate", "date", "year");

            if (name == null || artist == null || album == null || durationSeconds == null || year == null) {
                throw new IllegalArgumentException("Invalid MP3");
            }

            return new ExtractedMetadata(name, artist, album, durationSeconds, year);
        } catch (IllegalArgumentException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid MP3");
        }
    }

    private String firstNonBlank(Metadata metadata, String... keys) {
        for (String key : keys) {
            String value = metadata.get(key);
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return null;
    }

    public record ExtractedMetadata(
            String name,
            String artist,
            String album,
            String durationSeconds,
            String year
    ) {
    }
}

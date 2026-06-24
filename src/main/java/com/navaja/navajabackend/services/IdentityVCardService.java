package com.navaja.navajabackend.services;

import com.navaja.navajabackend.dto.IdentityRequest;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
public class IdentityVCardService {

    public byte[] generateIdentityVCard(IdentityRequest request) {
        StringBuilder vcard = new StringBuilder();

        vcard.append("BEGIN:VCARD\n");
        vcard.append("VERSION:3.0\n");

        String nombre = request.getNombre() != null ? request.getNombre() : "";
        vcard.append("N:").append(nombre).append(";;;;\n");
        vcard.append("FN:").append(nombre).append("\n");

        if (request.getEmpresa() != null && !request.getEmpresa().isEmpty()) {
            vcard.append("ORG:").append(request.getEmpresa()).append("\n");
        }

        if (request.getCargo() != null && !request.getCargo().isEmpty()) {
            vcard.append("TITLE:").append(request.getCargo()).append("\n");
        }

        if (request.getTelefono() != null && !request.getTelefono().isEmpty()) {
            vcard.append("TEL;TYPE=WORK,VOICE:").append(request.getTelefono()).append("\n");
        }

        if (request.getEmail() != null && !request.getEmail().isEmpty()) {
            vcard.append("EMAIL;TYPE=PREF,INTERNET:").append(request.getEmail()).append("\n");
        }

        if (request.getSitioWeb() != null && !request.getSitioWeb().isEmpty()) {
            vcard.append("URL:").append(request.getSitioWeb()).append("\n");
        }

        if (request.getDescripcion() != null && !request.getDescripcion().isEmpty()) {
            vcard.append("NOTE:").append(request.getDescripcion().replace("\n", "\\n")).append("\n");
        }

        // Add social URLs as additional URL fields or X-SOCIALPROFILE
        addSocial(vcard, "LinkedIn", request.getLinkedin());
        addSocial(vcard, "Twitter", request.getTwitter());
        addSocial(vcard, "GitHub", request.getGithub());
        addSocial(vcard, "TikTok", request.getTiktok());
        addSocial(vcard, "YouTube", request.getYoutube());
        addSocial(vcard, "Twitch", request.getTwitch());
        addSocial(vcard, "Discord", request.getDiscord());
        addSocial(vcard, "Reddit", request.getReddit());
        addSocial(vcard, "Behance", request.getBehance());
        addSocial(vcard, "Dribbble", request.getDribbble());
        addSocial(vcard, "GitLab", request.getGitlab());
        addSocial(vcard, "Medium", request.getMedium());
        addSocial(vcard, "Dev.to", request.getDevto());
        addSocial(vcard, "StackOverflow", request.getStackoverflow());

        vcard.append("END:VCARD\n");

        return vcard.toString().getBytes(StandardCharsets.UTF_8);
    }

    private void addSocial(StringBuilder vcard, String platform, String url) {
        if (url != null && !url.isEmpty()) {
            // X-SOCIALPROFILE is supported by Apple, or we can just use URL
            vcard.append("URL;type=").append(platform).append(":").append(url).append("\n");
        }
    }
}

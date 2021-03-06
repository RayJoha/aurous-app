package me.aurous.apis.impl.vkapi.audio;

import java.io.IOException;

import me.aurous.apis.impl.vkapi.VKApi;

public class AudioApi extends VKApi {

	public AudioApi(final int applicationId, final String formData) {
		super(applicationId, formData);
	}

	public AudioApi(final VKApi api) {
		super(api);
	}

	public String getAllMyAudioJson() throws IOException {
		return submitQuery("audio.get", "");
	}

	public String searchAudioByIdJson(final String paramaters)
			throws IOException {
		return submitQuery("audio.getById", paramaters);
	}

	public String searchAudioJson(final String paramaters) throws IOException {
		return submitQuery("audio.search", paramaters);
	}

}
import groovy.json.JsonOutput
import org.gradle.api.DefaultTask
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction

import java.net.HttpURLConnection

/**
 * Posts a message to a Discord incoming webhook.
 *
 * Configuration (set via environment variables or Gradle properties):
 *   DISCORD_WEBHOOK_URL / discordWebhookUrl  — webhook URL (required)
 *   DISCORD_AVATAR_URL  / discordAvatarUrl   — bot avatar override (optional)
 *   DISCORD_USERNAME    / discordUsername    — bot name override (optional)
 *   DISCORD_MESSAGE_FORMAT / discordMessageFormat — message template (required)
 *
 * The message template may contain {placeholder} tokens that are resolved
 * against the {@link #getPlaceholders()} map before sending.
 */
abstract class DiscordWebhookTask extends DefaultTask {

    /** Discord incoming-webhook URL. */
    @Input
    abstract Property<String> getWebhookUrl()

    /** Override the bot avatar image URL (optional). */
    @Input
    @Optional
    abstract Property<String> getAvatarUrl()

    /** Override the bot display name (optional). */
    @Input
    @Optional
    abstract Property<String> getUsername()

    /**
     * Message template.
     * Use {key} tokens that are substituted from {@link #getPlaceholders()}.
     * Example: "Version {version} released! Fabric: {download_url_fabric}"
     */
    @Input
    abstract Property<String> getMessageFormat()

    /**
     * Map of placeholder keys to replacement values.
     * All {key} occurrences in {@link #getMessageFormat()} are replaced.
     */
    @Input
    abstract MapProperty<String, String> getPlaceholders()

    @TaskAction
    void send() {
        // Resolve placeholders in the message
        def content = messageFormat.get()
        placeholders.get().each { key, value ->
            content = content.replace("{${key}}", value ?: '')
        }

        // Build JSON payload
        def payload = [content: content]
        if (avatarUrl.isPresent()) payload.avatar_url = avatarUrl.get()
        if (username.isPresent())  payload.username   = username.get()

        def json = JsonOutput.toJson(payload)
        logger.info("Sending Discord webhook payload: {}", json)

        // POST to webhook
        def conn = new URI(webhookUrl.get()).toURL().openConnection() as HttpURLConnection
        conn.requestMethod = 'POST'
        conn.setRequestProperty('Content-Type', 'application/json; charset=UTF-8')
        conn.doOutput = true
        conn.outputStream.withWriter('UTF-8') { it << json }

        def code = conn.responseCode
        if (code < 200 || code >= 300) {
            def error = conn.errorStream?.withReader('UTF-8') { it.text } ?: '(no body)'
            throw new RuntimeException("Discord webhook returned HTTP ${code}: ${error}")
        }
        logger.lifecycle("Discord notification sent (HTTP ${code})")
    }
}

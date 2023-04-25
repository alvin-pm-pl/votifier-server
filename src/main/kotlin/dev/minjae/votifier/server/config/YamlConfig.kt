package dev.minjae.votifier.server.config

import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.Yaml
import java.io.File
import java.io.InputStream
import java.nio.file.Path

class YamlConfig : Configuration {
    constructor(file: String?) : super(file)
    constructor(path: Path?) : super(path!!)
    constructor(saveFile: File?) : super(saveFile)
    constructor(saveFile: File?, inputStream: InputStream?) : super(saveFile, inputStream)

    override fun deserialize(inputStream: InputStream?): MutableMap<String?, Any?> {
        return yaml.loadAs(inputStream, MutableMap::class.java)
    }

    override fun serialize(values: Map<String?, Any?>?): String {
        return yaml.dump(values)
    }

    companion object {
        private val yaml: Yaml

        init {
            val dumperOptions = DumperOptions()
            dumperOptions.defaultFlowStyle = DumperOptions.FlowStyle.BLOCK
            dumperOptions.isPrettyFlow = true
            yaml = Yaml(dumperOptions)
        }
    }
}

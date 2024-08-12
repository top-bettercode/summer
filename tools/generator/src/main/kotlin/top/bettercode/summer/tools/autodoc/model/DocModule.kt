package top.bettercode.summer.tools.autodoc.model

import org.springframework.http.MediaType
import top.bettercode.summer.tools.autodoc.AutodocExtension
import top.bettercode.summer.tools.autodoc.model.DocCollection.Companion.write
import top.bettercode.summer.tools.autodoc.operation.DocOperation
import top.bettercode.summer.tools.autodoc.postman.Event
import top.bettercode.summer.tools.autodoc.postman.Script
import top.bettercode.summer.tools.lang.operation.Operation
import java.io.File

/**
 *
 * @author Peter Wu
 */
data class DocModule(val rootModuleDic: File?, val projectModuleDic: File?) {
    private val log = org.slf4j.LoggerFactory.getLogger(DocModule::class.java)

    private val collectionsFile: File = File(projectModuleDic, "collections.yml")
    val collections: LinkedHashSet<ICollection>
    private val rootCollections: LinkedHashSet<DocCollection>
    private val projectCollections: LinkedHashSet<DocCollection>

    init {
        require(!(projectModuleDic == null && rootModuleDic == null)) { "projectModuleDic,rootModuleDic不能同时为null" }

        collections = linkedSetOf()
        if (rootModuleDic?.exists() == true) {
            val rootCollectionsFile = File(rootModuleDic, "collections.yml")
            rootCollections = if (rootCollectionsFile.exists()) {
                DocCollection.read(rootCollectionsFile)
            } else {
                linkedSetOf()
            }
            rootCollections.forEach {
                collections.add(CombineCollection(it, null))
            }
        } else {
            rootCollections = linkedSetOf()
        }

        projectCollections = if (collectionsFile.exists()) {
            DocCollection.read(collectionsFile)
        } else {
            linkedSetOf()
        }
        projectCollections.forEach { collection ->
            val exist = collections.find { it.name == collection.name }
            if (exist == null) {
                collections.add(collection)
            } else {
                (exist as CombineCollection).projectCollection = collection
            }
        }
    }

    fun allModuleFiles(action: (File) -> Collection<File>): Collection<File> {
        val result: MutableCollection<File> = mutableListOf()
        if (projectModuleDic?.exists() == true)
            result.addAll(action(projectModuleDic))
        if (this.rootModuleDic?.exists() == true)
            result.addAll(action(this.rootModuleDic))
        return result
    }

    val name: String = moduleFile { it.name }

    fun <T> moduleFile(action: (File) -> T): T {
        return if (projectModuleDic?.exists() == true)
            action(projectModuleDic)
        else
            action(rootModuleDic!!)
    }

    fun collections(collectionName: String, name: String): DocCollection {
        var collectionTree = projectCollections.find { it.name == collectionName }
        if (collectionTree == null) {
            collectionTree = DocCollection(
                collectionName,
                dir = File(projectModuleDic, "collection/$collectionName")
            )
            projectCollections.add(collectionTree)
        }
        collectionTree.items.add(name)
        return collectionTree
    }

    fun clean() {
        (rootCollections + projectCollections).forEach { collection ->
            val items = collection.items
            collection.dir.listFiles()
                ?.filterNot { items.contains(it.nameWithoutExtension) }
                ?.forEach {
                    it.delete()
                    log.warn("delete $it")
                }
        }

        if (this.rootModuleDic != null) {
            val rootCollectionNames = rootCollections.map { it.name }
            File(this.rootModuleDic, "collection").listFiles()
                ?.filterNot { rootCollectionNames.contains(it.name) }?.forEach {
                    it.deleteRecursively()
                    log.warn("delete $it")
                }
        }
        val subCollectionNames = projectCollections.map { it.name }
        File(projectModuleDic, "collection").listFiles()
            ?.filterNot { subCollectionNames.contains(it.name) }?.forEach {
                it.deleteRecursively()
                log.warn("delete $it")
            }
    }

    fun writeToDisk() {
        clean()
        projectCollections.write(collectionsFile)
    }

    fun postmanEvents(operation: DocOperation, autodoc: AutodocExtension): List<Event> {
        return listOf(
            Event(
                "prerequest",
                Script(exec = operation.prerequest.ifEmpty { defaultPrerequestExec(operation) })
            ), Event("test", Script(exec = operation.testExec.ifEmpty {
                defaultPostmanTestExec(operation, autodoc)
            }))
        )
    }

    private fun defaultPostmanTestExec(
        operation: Operation,
        autodoc: AutodocExtension
    ): List<String> {
        val statusCode = operation.response.statusCode
        val exec = mutableListOf<String>()
        val maxResponseTime = autodoc.maxResponseTime
        exec.add("pm.test('验证响应状态码是$statusCode', function () {")
        exec.add("  pm.response.to.have.status($statusCode);")
        exec.add("});")
        exec.add("")
        exec.add("pm.test('验证响应时间小于${maxResponseTime}ms', function () {")
        exec.add("  pm.expect(pm.response.responseTime).to.be.below($maxResponseTime);")
        exec.add("});")
        exec.add("")
        if (operation.response.contentType?.isCompatibleWith(MediaType.APPLICATION_JSON) == true) {
            exec.add("pm.test('验证返回json格式', function () {")
            exec.add("  pm.response.to.be.json;")
            if (operation.request.restUri == autodoc.authUri) {
                exec.add("  var jsonData = pm.response.json();")
                autodoc.authVariables.forEach {
                    exec.add("  pm.globals.set('${it.substringAfterLast('.')}', jsonData.$it);")
                }
            }
            exec.add("});")
            exec.add("")
        }
        return exec
    }

    /**
     * @param operation operation
     */
    private fun defaultPrerequestExec(operation: Operation): List<String> {
        val exec = mutableListOf<String>()
        operation.request.apply {
            operation.request.uriVariables.forEach { (t, u) ->
                exec.add("pm.globals.set('$t', '$u');")
            }
        }
        return exec
    }

}
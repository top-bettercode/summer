package cn.bestwu.autodoc.core.model

import cn.bestwu.autodoc.core.AutodocExtension
import cn.bestwu.autodoc.core.operation.DocOperation
import cn.bestwu.autodoc.core.postman.Event
import cn.bestwu.autodoc.core.postman.Script
import cn.bestwu.autodoc.core.readCollections
import cn.bestwu.autodoc.core.writeCollections
import cn.bestwu.logging.operation.Operation
import org.springframework.http.MediaType
import java.io.File

/**
 *
 * @author Peter Wu
 */
data class DocModule(val rootModuleDic: File?, val projectModuleDic: File?) {
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
                rootCollectionsFile.readCollections()
            } else {
                linkedSetOf()
            }
            rootCollections.forEach {
                it.dir = File(rootModuleDic, "collection/${it.name}")
                collections.add(CombineCollection(it, null))
            }
        } else {
            rootCollections = linkedSetOf()
        }

        projectCollections = if (collectionsFile.exists()) {
            collectionsFile.readCollections()
        } else {
            linkedSetOf()
        }
        projectCollections.forEach { collection ->
            collection.dir = File(projectModuleDic, "collection/${collection.name}")
            val exist = collections.find { it.name == collection.name }
            if (exist == null) {
                collections.add(collection)
            } else {
                (exist as CombineCollection).projectCollection = collection
            }
        }
    }

    private fun allModuleFiles(action: (File) -> Unit) {
        if (projectModuleDic?.exists() == true)
            action(projectModuleDic)
        if (this.rootModuleDic?.exists() == true)
            action(this.rootModuleDic)
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
            collectionTree = DocCollection(collectionName)
            collectionTree.dir = File(projectModuleDic, "collection/$collectionName")
            projectCollections.add(collectionTree)
        }
        collectionTree.items.add(name)
        return collectionTree
    }

    private fun clean() {
        (rootCollections + projectCollections).forEach { collection ->
            val items = collection.items
            collection.dir.listFiles()?.filterNot { items.contains(it.nameWithoutExtension) || it.name == "field.yml" }?.forEach { it.delete() }
        }

        if (this.rootModuleDic != null) {
            val rootCollectionNames = rootCollections.map { it.name }
            File(this.rootModuleDic, "collection").listFiles()?.filterNot { rootCollectionNames.contains(it.name) }?.forEach {
                it.deleteRecursively()
            }
        }
        val subCollectionNames = projectCollections.map { it.name }
        File(projectModuleDic, "collection").listFiles()?.filterNot { subCollectionNames.contains(it.name) }?.forEach {
            it.deleteRecursively()
        }
    }

    fun writeToDisk() {
        clean()
        collectionsFile.writeCollections(projectCollections)
    }

    fun postmanEvents(operation: DocOperation, autodoc: AutodocExtension): List<Event> {
        return listOf(Event("prerequest", Script(exec = if (operation.prerequest.isNotEmpty()) operation.prerequest else defaultPrerequestExec(operation))), Event("test", Script(exec = if (operation.testExec.isNotEmpty()) {
            operation.testExec
        } else {
            defaultPostmanTestExec(operation, autodoc)
        })))
    }

    private fun defaultPostmanTestExec(operation: Operation, autodoc: AutodocExtension): List<String> {
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
        if (operation.response.headers.contentType?.isCompatibleWith(MediaType.APPLICATION_JSON) == true) {
            exec.add("pm.test('验证返回json格式', function () {")
            exec.add("  pm.response.to.be.json;")
            if (autodoc.wrapResponse || operation.request.restUri == autodoc.authUri)
                exec.add("  var jsonData = pm.response.json();")
            if (autodoc.wrapResponse)
                exec.add("  pm.expect(jsonData.status).to.equal(\"200\");")
            if (operation.request.restUri == autodoc.authUri)
                autodoc.authVariables.forEach {
                    exec.add("  pm.globals.set('${it.substringAfterLast('.')}', jsonData.$it);")
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
package top.bettercode.summer.test.autodoc

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.ok
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import top.bettercode.summer.security.authorize.Anonymous
import javax.servlet.http.HttpServletResponse
import javax.validation.groups.Default


interface Create : Default

/**
 * @author Peter Wu
 */
@ImportAutoConfiguration(AutodocConfiguration::class, DataSourceAutoConfiguration::class)
@SpringBootApplication
@RestController
@RequestMapping(name = "客户端TOKEN")
@Anonymous
class ClientTokenController {

    @GetMapping(value = ["/clientTokens"], name = "列表")
    fun index(): Any {
        val results = listOf(insert())
        return ok(
                mapOf(
                        "page" to 1,
                        "size" to 20,
                        "list" to results
                )
        )
    }

    @PostMapping(value = ["/oauth/token"], name = "token")
    fun token(): Any {
        val results = listOf(insert())
        return ok(results)
    }

    @GetMapping(value = ["/clientTokens/{authenticationId}"], name = "详情")
    fun show(@PathVariable authenticationId: String): Any {
        return ok(insert())
    }

    @PostMapping(value = ["/clientTokens"], name = "新增")
    fun create(@Validated @RequestBody clientToken: Array<ClientToken>): Any {
        return ResponseEntity.status(HttpStatus.CREATED).body("")
    }

    @PutMapping(value = ["/clientTokens/{authenticationId}"], name = "编辑")
    fun update(@Validated clientToken: ClientToken): Any {
        return ok(insert())
    }

    @DeleteMapping(value = ["/clientTokens/{authenticationId}"], name = "删除")
    fun delete(@PathVariable authenticationId: String): Any {
        return ResponseEntity.noContent().build<Any>()
    }

    //上传文件,下载文件
    @PostMapping(value = ["/clientTokens/upload"], name = "上传文件")
    fun upload(file: MultipartFile, response: HttpServletResponse) {
        response.contentType = "application/octet-stream"
        response.setHeader("Content-Disposition", "attachment; filename=\"${file.originalFilename}\"")
        response.outputStream.write(file.bytes)
        response.outputStream.flush()
    }


    private fun insert(): ClientToken {
        val clientToken = ClientToken()
        clientToken.tokenId = "test"
        clientToken.token = ByteArray(0)
        clientToken.authenticationId = "test"
        clientToken.userName = "test"
        clientToken.clientId = 12
        return clientToken
    }
}

fun main(args: Array<String>) {
    SpringApplication.run(ClientTokenController::class.java, *args)
}
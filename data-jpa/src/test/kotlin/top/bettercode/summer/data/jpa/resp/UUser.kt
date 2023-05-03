package top.bettercode.summer.data.jpa.resp

/**
 * @author Peter Wu
 */
class UUser {
    var firstName: String? = null
    var lastName: String? = null

    constructor()
    constructor(firstName: String?, lastName: String?) {
        this.firstName = firstName
        this.lastName = lastName
    }
}

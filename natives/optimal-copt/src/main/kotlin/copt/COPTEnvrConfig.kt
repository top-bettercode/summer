package copt

import top.bettercode.summer.tools.optimal.copt.COPTNativeLibLoader

class COPTEnvrConfig {
    private var _self: IEnvrConfig

    constructor() {
        this._self = CoptJniWrapper.CreateEnvrConfig()
        CoptException.checkError(_self.GetLastError(), _self.GetErrorMessage())
    }

    fun dispose() {
        this.finalize()
    }

    protected fun finalize() {
        _self.finalize()
    }

    @Throws(CoptException::class)
    fun set(var1: String?, var2: String?) {
        _self.Set(var1, var2)
        CoptException.checkError(_self.GetLastError(), _self.GetErrorMessage())
    }

    constructor(var1: IEnvrConfig) {
        this._self = var1
    }

    fun get(): IEnvrConfig {
        return this._self
    }

    companion object {
        init {
            COPTNativeLibLoader.loadNativeLib()
        }
    }

}

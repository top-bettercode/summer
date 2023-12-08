package copt

import top.bettercode.summer.tools.optimal.solver.COPTNativeLibLoader

class COPTEnvr {
    private var _self: IEnvr

    constructor() {
        this._self = CoptJniWrapper.CreateEnvr(null as ILogCallback?)
        CoptException.checkError(_self.GetLastError(), _self.GetErrorMessage())
    }

    constructor(var1: String?) {
        this._self = CoptJniWrapper.CreateEnvrWithPath(var1, null as ILogCallback?)
        CoptException.checkError(_self.GetLastError(), _self.GetErrorMessage())
    }

    constructor(var1: EnvrConfig) {
        this._self = CoptJniWrapper.CreateEnvrWithConfig(var1.get(), null as ILogCallback?)
        CoptException.checkError(_self.GetLastError(), _self.GetErrorMessage())
    }

    constructor(var1: IEnvr) {
        this._self = var1
    }

    fun dispose() {
        this.finalize()
    }

    protected fun finalize() {
        _self.finalize()
    }

    @Throws(CoptException::class)
    fun createModel(var1: String?): Model {
        val var2 = coptjniwrapJNI.IEnvr_CreateModel(IEnvr.getCPtr(this._self), this._self, var1)
        val var4 = if (var2 == 0L) null else IModel(var2, true)
        val var5 = Model(var4)
        CoptException.checkError(_self.GetLastError(), _self.GetErrorMessage())
        return var5
    }

    @Throws(CoptException::class)
    fun close() {
        _self.Close()
        CoptException.checkError(_self.GetLastError(), _self.GetErrorMessage())
    }

    fun get(): IEnvr {
        return this._self
    }

    companion object {
        init {
            COPTNativeLibLoader.loadNativeLib()
        }
    }
}

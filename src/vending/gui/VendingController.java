package vending.gui;

/**
 * Contrato entre la interfaz Swing y la lógica del proyecto.
 *
 * Los compañeros pueden implementar esta interfaz en el módulo
 * que conecte autómata, parser, lexer y modelo.
 */
public interface VendingController {

    void insertMoney(int amount);

    void selectProduct(String code);

    void buy();

    void cancel();

    /**
     * Analiza el texto actual del editor.
     * El resultado puede reflejarse en getViewModel() o en el mecanismo
     * que el equipo defina para los resultados del analizador.
     */
    void analyzeSource(String source);

    /**
     * Ejecuta una instrucción válida pendiente.
     */
    void executeNextInstruction();

    VendingViewModel getViewModel();
}

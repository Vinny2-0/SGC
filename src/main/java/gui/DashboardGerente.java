package main.java.gui;

import main.java.assistentes.IniciarGUI;
import main.java.entidades.Carro;
import main.java.entidades.Cliente;
import main.java.entidades.Funcionario;
import main.java.entidades.Venda;
import main.java.interfaces.FrameInterface;
import main.java.interfaces.PersistirDados;
import main.res.valores.Dimensoes;
import main.res.valores.Referencias;
import main.res.valores.Strings;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

public class DashboardGerente implements FrameInterface, PersistirDados {

    public JPanel panel1;
    private JTable tabelaVendas;
    private JTable tabelaEstoque;
    private JTextField textField2;
    private JTextField textField3;
    private JLabel img;
    private JButton cadastrarFuncionarioButton;
    private JButton comprarVeiculoButton;
    private JLabel ola;
    private JButton sairButton;
    private static double TOTAL;
    private static int ESTOQUE;
    private static int VENDIDOS;
    private JLabel lucroTotal;
    private JLabel carrosEstoque;
    private JLabel carrosVendidos;
    private JLabel vendasDoDia;

    public DashboardGerente() {
        super();

        assert false;
        img.setIcon(icone);
        lucroTotal.setText("");

        ola.setText(String.format("Olá %s!", Referencias.FUNCIONARIO.getNome()));

        if (!Referencias.FUNCIONARIO.getAcesso()) {
            cadastrarFuncionarioButton.setEnabled(false);
            comprarVeiculoButton.setEnabled(false);
        } else {
            cadastrarFuncionarioButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    IniciarGUI.show(Referencias.CADASTRAR_FUNCIONARIO);
                }
            });

            comprarVeiculoButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    IniciarGUI.show(Referencias.COMPRAR_CARRO);
                }
            });
        }

        sairButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                Referencias.FUNCIONARIO = null;
                IniciarGUI.show(Referencias.LOGIN);
            }
        });

        configuraTabelaVendas();
        configuraTabelaEstoque();
        vendasDia();
    }

    private void configuraTabelaVendas() {
        ArrayList<Venda> vendas = getVendas();
        long i = 0;

        DefaultTableModel model = new DefaultTableModel() {

            @Override
            public boolean isCellEditable(int i, int i1) {
                return false;
            }

        };
        TOTAL = 0.0;
        for (Object name : Referencias.COLUNAS_VENDAS) {
            model.addColumn(name);
        }

        for (Venda venda : vendas) {
            Object[] objects = {
                    venda.getId(),
                    Objects.requireNonNull(getCliente(venda.getClienteID())).getNome(),
                    venda.gettipoPagamento(),
                    Objects.requireNonNull(getCarro(venda.getCarroID())).getModelo(),
                    Objects.requireNonNull(getVendedor(venda.getFuncionarioID())).getNome()};

            model.addRow(objects);

            TOTAL += Objects.requireNonNull(getCarro(venda.getCarroID())).getPreco();
        }

        tabelaVendas.setModel(model);

        tabelaVendas.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int row = tabelaVendas.rowAtPoint(evt.getPoint());
                int col = tabelaVendas.columnAtPoint(evt.getPoint());
                if (row >= 0 && col >= 0) {

                    JOptionPane.showMessageDialog(frame, Objects.requireNonNull(getCliente(vendas.get(tabelaVendas.getSelectedRow()).getClienteID())).toMap().toString()
                            .replace(",", "\n")
                            .replace("{", "")
                            .replace("}", "")
                            .replace("=", ": ")
                            .toUpperCase());
                }
            }
        });

        lucroTotal.setText(String.valueOf(TOTAL));
    }

    private void configuraTabelaEstoque() {
        ArrayList<Carro> carros = getCarros();

        DefaultTableModel model = new DefaultTableModel() {

            @Override
            public boolean isCellEditable(int i, int i1) {
                return false;
            }

        };

        for (Object name : Referencias.COLUNAS_ESTOQUE) {
            model.addColumn(name);
        }

        for (Carro carro : carros) {
            if (!carro.isVendido()) {
                Object[] objects = {carro.getId(), carro.getMarca(), carro.getModelo(), carro.getAno(), carro.getPreco() * 1000};
                model.addRow(objects);

                ESTOQUE++;
            } else {
                VENDIDOS++;
            }
        }

        tabelaEstoque.setModel(model);

        tabelaEstoque.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int row = tabelaEstoque.rowAtPoint(evt.getPoint());
                int col = tabelaEstoque.columnAtPoint(evt.getPoint());
                if (row >= 0 && col >= 0) {

                    JOptionPane.showMessageDialog(frame, Objects.requireNonNull(getCarro(carros.get(tabelaEstoque.getSelectedRow()).getId())).toMap().toString()
                            .replace(",", "\n")
                            .replace("{", "")
                            .replace("}", "")
                            .replace("=", ": ")
                            .toUpperCase());
                }
            }
        });

        carrosEstoque.setText(String.valueOf(ESTOQUE));
        carrosVendidos.setText(String.valueOf(VENDIDOS));
    }

    private Cliente getCliente(long id) {

        ArrayList<Cliente> clientes = getClientes();

        for (Cliente cliente : clientes) {
            if (cliente.getId() == id) {
                return cliente;
            }
        }

        return null;
    }

    private Carro getCarro(long id) {

        ArrayList<Carro> carros = getCarros();

        for (Carro carro : carros) {
            if (carro.getId() == id) {
                return carro;//.getModelo() + " (" + carro.getMarca() + ")";
            }
        }

        return null;
    }

    private Funcionario getVendedor(long id) {

        ArrayList<Funcionario> funcionarios = getFuncionarios();

        for (Funcionario funcionario : funcionarios) {
            if (funcionario.getId() == id) {
                return funcionario;
            }
        }

        return null;
    }

    private void vendasDia() {
        ArrayList<Venda> vendas = getVendas();
        ArrayList<Carro> carro = getCarros();
        double somaVendas = 0.0;
        for (Venda item : vendas) {

            for (Carro itemCarro : carro) {
                if (item.getCarroID() == itemCarro.getId()) {
                    DateFormat df = new SimpleDateFormat("dd:MM:yyyy");
                    Calendar cal = Calendar.getInstance();
                    cal.setTimeInMillis(item.getId());
                    String[] data = df.format(cal.getTime()).split(":");
                    Date now = new Date();
                    Date copiedDate = new Date(now.getTime());
                    String[] dataDia = df.format(copiedDate).split(":");

                    if (data[2].equals(dataDia[2]) && data[1].equals(dataDia[1]) && data[0].equals(dataDia[0])) {
                        somaVendas = somaVendas + itemCarro.getPreco();
                    }
                }
            }
        }
        vendasDoDia.setText(String.valueOf(somaVendas * 1000));
    }

    @Override
    public void show() {
        frame.setContentPane(panel1);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setTitle(Strings.DAL_COMPLETO);
        frame.setSize(Dimensoes.DASH);
        frame.setLocation(Dimensoes.getCentroTela(frame.getWidth(), frame.getHeight()));

        try {
            frame.setIconImage(ImageIO.read(new File(Strings.ICONE)));
        } catch (IOException e) {
            e.printStackTrace();
        }

        frame.setVisible(true);
    }

    @Override
    public void hide() {
        frame.setVisible(false);
    }

}


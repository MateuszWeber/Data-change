package Data;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class Change extends JFrame implements ActionListener {

    JFileChooser wybierzPlik = new JFileChooser();
    JLabel szuk = new JLabel("Co szukamy?");
    JTextField szukanie = new JTextField();
    JLabel zmie = new JLabel("Na co zmieniamy?");
    JTextField zmienianie = new JTextField();
    JButton otworz = new JButton("Wybierz plik");
    JTextArea log = new JTextArea(5,20);

    String nazwa = null;

    public Change(){

        super("Zmiana w plikach tekstowych");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new FlowLayout());
        setPreferredSize(new Dimension(400,300));

        JPanel dane = new JPanel();
        JPanel dane2 = new JPanel();
        szukanie.setColumns(20);
        zmienianie.setColumns(20);
        dane.add(szuk);
        dane.add(szukanie);
        add(dane);
        dane2.add(zmie);
        dane2.add(zmienianie);
        add(dane2);

        add(otworz);
        otworz.addActionListener(this);

        log.setEditable(false);
        add(log);
        setVisible(true);
        pack();

        this.setDropTarget(new DropTarget() {
            public synchronized void drop(DropTargetDropEvent evt) {
                try {
                    evt.acceptDrop(DnDConstants.ACTION_COPY);
                    List<File> droppedFiles = (List<File>) evt
                            .getTransferable().getTransferData(
                                    DataFlavor.javaFileListFlavor);
                    for (File file : droppedFiles) {
                        nazwa = file.getName();
                        dzialanieNaPliku();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    void dzialanieNaPliku(){

        Path patch = Paths.get(nazwa);
        String wartoscSzukana = szukanie.getText();
        String wartoscOczekiwana = zmienianie.getText();

        try {
            byte[] bytes = Files.readAllBytes(patch);
            log.append("Otwarto plik: " + nazwa + "." + "\n");
            byte [] ciag = wartoscSzukana.getBytes();
            byte [] ciagOczekiwany = wartoscOczekiwana.getBytes();

            for(int i = 0; i < bytes.length - ciag.length+1; ++i) {
                boolean found = true;
                for(int j = 0; j < ciag.length; ++j) {
                    if (bytes[i+j] != ciag[j]) {
                        found = false;
                        break;
                    }
                }
                if (found){
                    log.append("Znaleziono ciag: " + Integer.toHexString(i) + " " + wartoscSzukana + "\n");
                    for (int j = 0; j < ciagOczekiwany.length; j++){
                        bytes[i + j] = ciagOczekiwany[j];
                    }
                    log.append("Zmieniono na ciag: " + Integer.toHexString(i) + " " + wartoscOczekiwana + "\n");
                }
            }
            try (FileOutputStream fos = new FileOutputStream("MOD_" + nazwa)) {
                fos.write(bytes);
                log.append("Zapisano zmiany w pliku: MOD_" + nazwa + "." + "\n");
            }
        } catch (IOException e) {
            log.append("B³¹d otwarcia pliku: " + nazwa + "." + "\n");
        }
        log.append("Zamnknieto plik: " + nazwa + "." + "\n");
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        if (e.getSource() == otworz){
            int returnVal = wybierzPlik.showOpenDialog(this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = wybierzPlik.getSelectedFile();
                nazwa = file.getName();
                dzialanieNaPliku();
            } else {
                log.append("Anulowano otwarcie pliku." + "\n");
            }
            log.setCaretPosition(log.getDocument().getLength());
        }
    }
}
